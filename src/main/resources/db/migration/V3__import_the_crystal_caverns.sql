-- The conditional inserts make this safe for local databases where the book
-- was imported manually before this migration was added.
INSERT INTO adventure_books (title, author, difficulty)
SELECT 'The Crystal Caverns',
       'Evelyn Stormrider',
       'EASY' WHERE NOT EXISTS (
    SELECT 1 FROM adventure_books
    WHERE title = 'The Crystal Caverns' AND author = 'Evelyn Stormrider'
);

INSERT INTO sections (book_id, section_number, text, type)
SELECT book.id, section_data.section_number, section_data.text, section_data.type
FROM adventure_books book
         CROSS JOIN (VALUES (1::BIGINT,
                             'You stand at the entrance of the legendary Crystal Caverns. A cold breeze carries whispers from the darkness below. A broken rope bridge leads into the depths.',
                             'BEGIN'),
                            (20,
                             'Your hands brush against the cold stone until you find a hidden crevice leading downward. It''s dark, but you hear faint dripping water.',
                             'NODE'),
                            (100,
                             'The bridge creaks under your weight. Halfway across, a plank snaps beneath your foot!',
                             'NODE'),
                            (200,
                             'You emerge into a glittering chamber filled with glowing crystals. At the center lies a pedestal holding an ancient gemstone.',
                             'NODE'),
                            (300,
                             'You crawl to safety on the other side, breathing heavily. A winding path descends deeper into the cavern.',
                             'NODE'),
                            (400,
                             'You barely make the jump, but the rope bridge collapses behind you. There''s no turning back now.',
                             'NODE'),
                            (500,
                             'The moment you touch the gemstone, the chamber begins to shake violently. A secret door opens to reveal a hidden passage.',
                             'NODE'),
                            (600,
                             'You reach a vast underground lake glowing with bioluminescent crystals. A small boat waits at the shore.',
                             'NODE'),
                            (700, 'After a short rest, you feel strong enough to continue your journey.', 'NODE'),
                            (800,
                             'The hidden passage leads to a giant crystal throne room, where an ancient spirit awaits.',
                             'END'),
                            (900,
                             'The boat carries you safely across, but you sense something massive moving in the depths below.',
                             'NODE'),
                            (1000,
                             'You manage to swim across, shivering but alive. The glowing crystals ahead light your way to a massive door.',
                             'END'),
                            (666, 'An unreachable dead end. You shouldn''t be here.',
                             'NODE')) AS section_data(section_number, text, type)
WHERE book.title = 'The Crystal Caverns'
  AND book.author = 'Evelyn Stormrider'
  AND NOT EXISTS (SELECT 1 FROM sections WHERE book_id = book.id);

INSERT INTO options (section_id, description, goto_id, consequence_type, consequence_value, consequence_text)
SELECT source_section.id,
       option_data.description,
       option_data.goto_id,
       option_data.consequence_type,
       option_data.consequence_value,
       option_data.consequence_text
FROM adventure_books book
         JOIN sections source_section ON source_section.book_id = book.id
         JOIN (VALUES (1::BIGINT, 'Cross the rope bridge carefully', 100::BIGINT, NULL::VARCHAR, NULL::INTEGER,
                       NULL::TEXT),
                      (1, 'Search the rocky walls for another path', 20, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (20, 'Enter the crevice', 200, 'LOSE_HEALTH', 4,
                       'You scrape your shoulder squeezing through the narrow gap.'),
                      (20, 'Return to the entrance', 1, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (100, 'Hold on tightly and crawl across', 300, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (100, 'Try to jump to the other side', 400, 'LOSE_HEALTH', 7,
                       'You land hard and twist your ankle.'),
                      (200, 'Take the gemstone', 500, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (200, 'Inspect the walls carefully', 600, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (300, 'Follow the path downward', 600, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (300, 'Rest and recover your strength', 700, 'GAIN_HEALTH', 3,
                       'You feel slightly better after resting.'),
                      (400, 'Continue deeper into the cavern', 600, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (500, 'Enter the hidden passage', 800, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (500, 'Drop the gemstone and run back', 200, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (600, 'Take the boat across the lake', 900, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (600, 'Swim across', 1000, 'LOSE_HEALTH', 5, 'The freezing water chills you to the bone.'),
                      (700, 'Head toward the underground lake', 600, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (900, 'Ignore it and continue forward', 800, NULL::VARCHAR, NULL::INTEGER, NULL::TEXT),
                      (900, 'Investigate the movement', 666, NULL::VARCHAR, NULL::INTEGER,
                       NULL::TEXT)) AS option_data(source_section_number, description, goto_id, consequence_type,
                                                   consequence_value, consequence_text)
              ON option_data.source_section_number = source_section.section_number
WHERE book.title = 'The Crystal Caverns'
  AND book.author = 'Evelyn Stormrider'
  AND NOT EXISTS (SELECT 1
                  FROM options existing_option
                           JOIN sections existing_section ON existing_section.id = existing_option.section_id
                  WHERE existing_section.book_id = book.id);
